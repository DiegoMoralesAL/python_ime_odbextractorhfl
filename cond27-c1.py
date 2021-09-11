from odbAccess import *
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.pyplot as plt1
import matplotlib.pyplot as plt2
import matplotlib.pyplot as plt3
import matplotlib.pyplot as plt4
import collections


def main():
    model = "Modelo C 27 Capa 1 NP"
    limit = -1
    nodeSetLimit = 140

    odb = openOdb(r'C:\Users\diego\PycharmProjects\Abaqus\cond27-c1new.odb')
    # odb = openOdb('E:\Simulaciones\ModeloC400-50.odb')
    # odb = openOdb('E:\Simulaciones\ModeloD400-50.odb')

    list = odb.steps['Step-1'].frames[len(odb.steps['Step-1'].frames) - 1].fieldOutputs['HFL'].values
    list2 = odb.steps['Step-1'].frames[len(odb.steps['Step-1'].frames) - 1].fieldOutputs['NT11'].values

    topTemperature = 400
    bottomTemperature = 25

    max = 0
    min = 0
    hflTuple = dict()
    for item in list:
        hflTuple[item.elementLabel] = item.data
        if item.data[1] > max:
            max = item.data[1]
        if item.data[1] < min:
            min = item.data[1]

    print("maxQ: " + str(max))
    print("minQ: " + str(min))

    max = 0
    min = 0
    temperatureTuple = dict()
    for item in list2:
        temperatureTuple[item.nodeLabel] = item.data
        if item.data > max:
            max = item.data
        if item.data < min:
            min = item.data

    print("maxT: " + str(max))
    print("minT: " + str(min))

    assembly = odb.rootAssembly
    name, instance = assembly.instances.items()[0]

    elements = instance.elements
    nodes = instance.nodes

    # gdzElset = instance.elementSets['GDZ'].elements
    # yszElset = instance.elementSets['YSZ-DENSE'].elements
    #
    # bondcoatElset = instance.elementSets['BC'].elements
    # sustratoElset = instance.elementSets['SUSTRATO'].elements
    # tgoElset = instance.elementSets['TGO'].elements
    #
    removeElements = []
    #
    # for elem in bondcoatElset:
    #     removeElements.append(elem.label)
    #
    # for elem in sustratoElset:
    #     removeElements.append(elem.label)
    #
    # for elem in tgoElset:
    #     removeElements.append(elem.label)

    # print(bondcoatElset)
    # print(len(bondcoatElset))
    # print(len(sustratoElset))
    # print(len(tgoElset))

    # print(len(removeElements))

    elementTuple = dict()
    nodeTuple = dict()
    nodeSetList = dict()

    for element in elements:
        elementTuple[element.label] = element.connectivity

    for node in nodes:
        nodeTuple[node.label] = node.coordinates

    print(model)
    print("elements " + str(len(elements)))
    print("nodes " + str(len(nodes)))

    minimumY = 200
    maximumY = 0
    minimumX = 200
    maximumX = 0

    for elementresult in list:
        elementLabel = elementresult.elementLabel

        if elementLabel not in removeElements:
            nodesElement = elementTuple[elementLabel]
            nodeTake = nodeTuple[nodesElement[0]]
            nodeTakeLabel = nodesElement[0]
            coordinateX = nodeTake[0]
            coordinateY = nodeTake[1]

            if coordinateY > maximumY:
                maximumY = coordinateY
            if coordinateY < minimumY:
                minimumY = coordinateY

            if coordinateX > maximumX:
                maximumX = coordinateX
            if coordinateX < minimumX:
                minimumX = coordinateX

            if coordinateX not in nodeSetList:
                nodeSetList[coordinateX] = []

            if nodeTakeLabel not in np.array(nodeSetList[coordinateX]):
                nodeSetList[coordinateX].append(nodeTakeLabel)

    print("minimun X: " + str(minimumX))
    print("maximum X: " + str(maximumX))
    print("minimun Y: " + str(minimumY))
    print("maximum Y: " + str(maximumY))

    elementSetList = dict()
    coordYSetList = dict()
    deltaTSetList = dict()
    coordYAsList = []
    coordXAsList = []

    count = 0
    for coordinateX, nodeSet in nodeSetList.items():
        if len(nodeSet) > nodeSetLimit:

            miny = 1000
            maxy = 0

            minNode = 0
            maxNode = 0

            # print(nodeSet)

            for nodeIndv in nodeSet:
                nodeCoord = nodeTuple[nodeIndv]
                if nodeCoord[1] > maxy:
                    maxy = nodeCoord[1]
                    maxNode = nodeIndv
                if nodeCoord[1] < miny:
                    miny = nodeCoord[1]
                    minNode = nodeIndv

            maxTemp = temperatureTuple[maxNode]
            minTemp = temperatureTuple[minNode]


            if (maxy - miny) * 1000000 > 0:
                if maxTemp != minTemp:
                    count = count + 1
                    print(count)

                    for elementCoordinateX in elements:
                        if len(elementCoordinateX.connectivity) == 4:
                            if (elementCoordinateX.connectivity[0] in np.array(nodeSet) or
                                    elementCoordinateX.connectivity[1] in np.array(nodeSet) or
                                    elementCoordinateX.connectivity[2] in np.array(nodeSet) or
                                    elementCoordinateX.connectivity[3] in np.array(nodeSet)):

                                if coordinateX not in elementSetList:
                                    elementSetList[coordinateX] = []

                                if elementCoordinateX.label not in elementSetList[coordinateX]:
                                    elementSetList[coordinateX].append(elementCoordinateX.label)
                                    coordXAsList.append(coordinateX * 1000000)
                                    coordYAsList.append((maxy - miny) * 1000000)
                                    coordYSetList[coordinateX] = (maxy - miny)
                                    deltaTSetList[coordinateX] = (maxTemp - minTemp)

            if count == limit:
                break

    hflAverages = []
    hflAveragesY = []
    hflAveragesY_DeltaT = []
    coordinateXPlot = []
    nodeSets = []


    for coordinateX, elementSet in elementSetList.items():
        countHFL = 0
        valueHFL = 0
        for elementInSet in elementSet:
            if np.array(hflTuple[elementInSet])[1] < 0:
                # print(str(hflTuple[elementInSet][1]))
                countHFL = countHFL + 1
                valueHFL = valueHFL + hflTuple[elementInSet]

        print(countHFL)
        valueHFL = valueHFL / countHFL

        # hflMagnitude = np.sqrt(valueHFL[0]*valueHFL[0] + valueHFL[1]*valueHFL[1])
        hflMagnitude = -valueHFL[1]
        hflMagnitude_Y = hflMagnitude * coordYSetList[coordinateX]
        hflMagnitude_Y__DT = hflMagnitude_Y / deltaTSetList[coordinateX]

        hflAverages.append(hflMagnitude)
        hflAveragesY.append(hflMagnitude_Y)
        hflAveragesY_DeltaT.append(hflMagnitude_Y__DT)
        nodeSets.append(countHFL)
        coordinateXPlot.append(coordinateX * 1000000)

    Ky = (np.array(hflAverages) * (maximumY-minimumY)) / (topTemperature - bottomTemperature)
    Ky2 = (np.array(hflAveragesY)) / (topTemperature - bottomTemperature)
    Ky3 = (np.array(hflAveragesY_DeltaT))

    # print(Ky)
    # print(coordinateXPlot)

    maxxinum = int(maximumX * 1000000)

    figure = plt.figure()
    plt.xticks(range(0, maxxinum, 50))
    plt.scatter(coordinateXPlot, Ky)
    plt.grid()
    figure.suptitle(model + ' Avg Ky ' + str(np.average(Ky)), fontsize=16)
    plt.xlabel('X Coordinate [um]', fontsize=10)
    plt.ylabel('Ky [W/Km]', fontsize=10)
    figure.savefig("Ky " + model + " " + str(count) + " NS " + str(nodeSetLimit) + '.png')
    # plt.show()

    figure3 = plt2.figure()
    plt.xticks(range(0, maxxinum, 50))
    plt2.scatter(coordinateXPlot, Ky2)
    plt2.grid()
    figure3.suptitle(model + ' Avg Ky2 ' + str(np.average(Ky2)), fontsize=16)
    plt2.xlabel('X Coordinate [um]', fontsize=10)
    plt2.ylabel('Ky [W/Km]', fontsize=10)
    figure3.savefig("Ky2 " + model + " " + str(count) + " NS " + str(nodeSetLimit) + '.png')
    # plt2.show()

    figure4 = plt3.figure()
    plt.xticks(range(0, maxxinum, 50))
    plt3.scatter(coordinateXPlot, Ky3)
    plt3.grid()
    figure4.suptitle(model + ' Avg Ky3 ' + str(np.average(Ky3)), fontsize=16)
    plt3.xlabel('X Coordinate [um]', fontsize=10)
    plt3.ylabel('Ky [W/Km]', fontsize=10)
    figure4.savefig("Ky3 " + model + " " + str(count) + " NS " + str(nodeSetLimit) + '.png')
    # plt3.show()

    coordplotSet = dict()
    for i in range(len(coordXAsList)):
        coordplotSet[coordXAsList[i]] = coordYAsList[i]

    ordcoordplotSet = collections.OrderedDict(sorted(coordplotSet.items()))
    coordXAsList = []
    coordYAsList = []
    for k, v in ordcoordplotSet.items():
        coordXAsList.append(k)
        coordYAsList.append(v)

    figure2 = plt1.figure()
    plt.xticks(range(0, maxxinum, 50))
    plt1.plot(coordXAsList, coordYAsList)
    plt1.grid()
    figure2.suptitle(model + ' NS ' + str(nodeSetLimit), fontsize=16)
    plt1.xlabel('X Coordinate [um]', fontsize=10)
    plt1.ylabel('Y Coordinate [um]', fontsize=10)
    figure2.savefig("XY " + model + " " + str(count) + " NS " + str(nodeSetLimit) + '.png')
    # plt1.show()

    figure5 = plt4.figure()
    plt4.xticks(range(0, maxxinum, 50))
    plt4.plot(coordXAsList, nodeSets)
    plt4.grid()
    figure5.suptitle(model + ' NS ' + str(nodeSetLimit), fontsize=16)
    plt4.xlabel('X Coordinate [um]', fontsize=10)
    plt4.ylabel('NodeSets', fontsize=10)
    figure5.savefig("NodeSets " + model + " " + str(count) + " NS " + str(nodeSetLimit) + '.png')
    # plt4.show()

    print("count: " + str(count))
    print(model + ", Ky 1: " + str(np.average(Ky)) + " std dev: " + str(np.std(Ky)))
    print(model + ", Ky 2: " + str(np.average(Ky2)) + " std dev: " + str(np.std(Ky2)))
    print(model + ", Ky 3: " + str(np.average(Ky3)) + " std dev: " + str(np.std(Ky3)))
    print(model + ", Node Set Avg: " + str(np.average(nodeSets)) + " std dev: " + str(np.std(nodeSets)))
    print(Ky3)

if __name__ == '__main__':
    main()
