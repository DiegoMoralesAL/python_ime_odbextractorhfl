from odbAccess import *
import time

def main():
    start_time = time.time()

    name_air = 'MAIR'

    odb = openOdb(r'C:\Users\diego\PycharmProjects\Abaqus\cond27-c1new.odb')

    list = odb.steps['Step-1'].frames[len(odb.steps['Step-1'].frames) - 1].fieldOutputs['HFL'].values
    list2 = odb.steps['Step-1'].frames[len(odb.steps['Step-1'].frames) - 1].fieldOutputs['NT11'].values

    assembly = odb.rootAssembly
    name, instance = assembly.instances.items()[0]
    print(instance.elementSets)

    # Write-Overwrites
    file1 = open("Abaqus Java//hfl.txt", "w")  # write mode
    for item in list:
        file1.write(str(item.elementLabel) + " " +str(item.data[1]) + "\n")
    file1.close()

    # Write-Overwrites
    file1 = open("Abaqus Java//nt11.txt", "w")  # write mode
    for item in list2:
        file1.write(str(item.nodeLabel) + " " + str(item.data) + "\n")
    file1.close()

    # Write-Overwrites
    file1 = open("Abaqus Java//elements.txt", "w")  # write mode
    for item in instance.elements:
        file1.write(str(item.label) + " " + str(item.connectivity) + "\n")
    file1.close()

    # Write-Overwrites
    file1 = open("Abaqus Java//nodes.txt", "w")  # write mode
    for item in instance.nodes:
        file1.write(str(item.label) + " " + str(item.coordinates) + "\n")
    file1.close()

    airElset = instance.elementSets[name_air].elements
    # Write-Overwrites
    file1 = open("Abaqus Java//airelems.txt", "w")  # write mode
    for item in airElset:
        file1.write(str(item.label) + "\n")
    file1.close()

    print("--- %s seconds ---" % (time.time() - start_time))

if __name__ == '__main__':
    main()
