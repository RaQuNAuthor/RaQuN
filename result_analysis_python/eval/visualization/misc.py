
def get_convergence_point(data: list):
    max_value = max(data)
    margin = max_value / 1000

    for index, value in enumerate(data):
        if abs(max_value - value) < margin:
            return index+1
    return len(data)+1


def get_overtake_point(data: list, limit: float):
    for index, value in enumerate(data):
        if value > limit:
            return index+1
    return len(data)+1


def get_real_name(method):
    name = method.replace("RaQuN_Weight_SmallVec", "RaQuN Low Dim.")
    name = name.replace("RaQuN_Weight", "RaQuN High Dim.")
    name = name.replace("PairwiseAsc", "Pairwise Ascending")
    name = name.replace("PairwiseDesc", "Pairwise Descending")
    return name


def get_real_dataset(dataset):
    name = dataset.replace("hospitals", "Hospital")
    name = name.replace("warehouses", "Warehouse")
    name = name.replace("random", "Random")
    name = name.replace("RandomLoose", "Loose")
    name = name.replace("RandomTight", "Tight")
    name = name.replace("ppu", "PPU")
    name = name.replace("bcms", "bCMS")
    name = name.replace("argouml", "ArgoUML")
    name = name.replace("Apogames", "Apo-Games")
    return name
