import os

from eval.visualization import bar_plots
from eval.data.result_loading import load_results
from eval.visualization.tables import create_tabular_overview, create_num_of_comp_overview, \
    create_model_stats_overview

# Change the path to evaluate different results
data_directory = "./../../reported-results"
experiment_subject_dir = "./../../experimental_subjects"
argo_dir = data_directory + "/argouml"
print(os.listdir("."))


def main():
    all_methods = os.listdir(data_directory)
    results_per_method = load_results(all_methods, data_directory)

    datasets_part_1 = [
        "hospitals"
        , "warehouses"
        , "random"
        , "randomLoose"
        , "randomTight"
    ]
    datasets_part_2 = [
        "ppu"
        , "bcms"
        , "argouml"
        , "Apogames"
    ]
    normal_methods = ["RaQuN_Weight_SmallVec",
                      "RaQuN_Weight",
                      "NwM",
                      "PairwiseAsc",
                      "PairwiseDesc"
                      ]
    subset_methods = ["RaQuN_Weight_SmallVec",
                      "RaQuN_Weight",
                      "NwM",
                      "PairwiseAsc",
                      "PairwiseDesc",
                      ]
    incremental_k_methods = ["RaQuN_k_Weight_SmallVec"]

    all_datasets = []
    all_datasets.extend(datasets_part_1)
    all_datasets.extend(datasets_part_2)
    argo_datasets = ["argouml_p001", "argouml_p005", "argouml_p010", "argouml_p015", "argouml_p020", "argouml_p025",
                     "argouml_p030", "argouml_p035", "argouml_p040", "argouml_p045", "argouml_p050", "argouml_p055",
                     "argouml_p060", "argouml_p065", "argouml_p070", "argouml_p075", "argouml_p080", "argouml_p085",
                     "argouml_p090", "argouml_p095", "argouml"]

    save_dir = "./../eval_results/tables/"
    from pathlib import Path
    Path(save_dir).mkdir(parents=True, exist_ok=True)

    tabular = create_tabular_overview(normal_methods, datasets_part_1, results_per_method)
    save_table(save_dir + "table_weight1.tex", tabular)
    print(tabular)
    print()
    print()
    tabular = create_tabular_overview(normal_methods, datasets_part_2, results_per_method)
    save_table(save_dir + "table_weight2.tex", tabular)
    print(tabular)
    print()
    print()
    tabular = create_num_of_comp_overview(all_datasets, results_per_method)
    save_table(save_dir + "table_comp.tex", tabular)
    print(tabular)
    print()
    print()

    bar_plots.create_runtime_plots(incremental_k_methods,
                                   list({"ppu", "bcms"} & set(datasets_part_2)),
                                   results_per_method, "F-Measure")

    if "argouml" in datasets_part_2:
        all_methods_argo = os.listdir(argo_dir)
        results_per_method_argo = load_results(all_methods_argo, argo_dir)

        bar_plots.create_runtime_plots(incremental_k_methods,
                                       ["argouml"],
                                       results_per_method_argo, "F-Measure")

        bar_plots.create_runtime_plot_argouml(subset_methods,
                                              argo_datasets, results_per_method_argo, use_legend=True)

        bar_plots.create_generic_plot_argouml(subset_methods,
                                              argo_datasets, results_per_method_argo, "Precision", use_legend=True)

        bar_plots.create_generic_plot_argouml(subset_methods,
                                              argo_datasets, results_per_method_argo, "Recall", use_legend=True)

    tabular = create_model_stats_overview(experiment_subject_dir, all_datasets)
    save_table(save_dir + "table_datasets.tex", tabular)
    print(tabular)
    print()
    print()


def save_table(path, table):
    with open(path, 'w') as file:
        file.writelines(table)


if __name__ == "__main__":
    main()
