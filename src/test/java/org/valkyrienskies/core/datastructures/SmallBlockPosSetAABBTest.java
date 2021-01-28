package org.valkyrienskies.core.datastructures;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.valkyrienskies.core.util.serialization.VSJacksonUtil;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmallBlockPosSetAABBTest {

    private static final ObjectMapper serializer = VSJacksonUtil.Companion.getDefaultMapper();

    @Test
    public void testSmallBlockPosSetAABB() {
        SmallBlockPosSetAABB toTest = new SmallBlockPosSetAABB(0, 0, 0, 1024, 1024, 1024);
        ExtremelyNaiveVoxelFieldAABBMaker aabbMaker = new ExtremelyNaiveVoxelFieldAABBMaker(0, 0);


        // Test adding new positions
        Vector3ic pos0 = new Vector3i(5, 10, 3);
        assertEquals(toTest.add(pos0), aabbMaker.addVoxel(pos0));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        Vector3ic pos1 = new Vector3i(2, 5, 3);
        assertEquals(toTest.add(pos1), aabbMaker.addVoxel(pos1));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        Vector3ic pos2 = new Vector3i(1, 20, 0);
        assertEquals(toTest.add(pos2), aabbMaker.addVoxel(pos2));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());


        // Test adding duplicates
        Vector3ic pos3 = new Vector3i(1, 20, 0);
        assertEquals(toTest.add(pos3), aabbMaker.addVoxel(pos3));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());


        // Test removing what doesn't exist
        Vector3ic pos4 = new Vector3i(6, 7, 8);
        assertEquals(toTest.remove(pos4), aabbMaker.removeVoxel(pos4));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());


        // Test removing what does exist
        Vector3ic pos5 = new Vector3i(5, 10, 3);
        assertEquals(toTest.remove(pos5), aabbMaker.removeVoxel(pos5));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        Vector3ic pos6 = new Vector3i(2, 5, 3);
        assertEquals(toTest.remove(pos6), aabbMaker.removeVoxel(pos6));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());

        Vector3ic pos7 = new Vector3i(1, 20, 0);
        assertEquals(toTest.remove(pos7), aabbMaker.removeVoxel(pos7));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());


        // Test adding new positions
        Vector3ic pos8 = new Vector3i(25, 2, 35);
        assertEquals(toTest.add(pos8), aabbMaker.addVoxel(pos8));
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());


        // Test clear
        toTest.clear();
        aabbMaker.clear();
        assertEquals(toTest.makeAABB(), aabbMaker.makeVoxelFieldAABB());
    }

    private static Stream<Arguments> coordsGenerator(final int centerX, final int centerZ) {
        final int testIterations = 500;
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        return IntStream.range(0, testIterations)
                .mapToObj(ignore -> {
                    int x = random.nextInt(-2048, 2047);
                    int y = random.nextInt(0, 255);
                    int z = random.nextInt(-2048, 2047);
                    return Arguments.arguments(centerX + x, y, centerZ + z, centerX, centerZ);
                });
    }

    /**
     * Tests the correctness of SmallBlockPosSetAABB serialization and deserialization.
     */
    @RepeatedTest(25)
    public void testSerializationAndDeSerialization() throws IOException {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int centerX = random.nextInt(Integer.MIN_VALUE + 2048, Integer.MAX_VALUE - 2047);
        final int centerY = 0; // Not very random :P
        final int centerZ = random.nextInt(Integer.MIN_VALUE + 2048, Integer.MAX_VALUE - 2047);

        final int xSize = 4096;
        final int ySize = 4096;
        final int zSize = 4096;

        final SmallBlockPosSetAABB blockPosSet = new SmallBlockPosSetAABB(centerX, centerY, centerZ, xSize, ySize, zSize);
        final Stream<Arguments> coordinatesGenerator = coordsGenerator(centerX, centerZ);

        coordinatesGenerator.forEach(pos -> blockPosSet.add((Integer) pos.get()[0], (Integer) pos.get()[1], (Integer) pos.get()[2]));

        // Now serialize and deserialize and verify that they are the same
        final byte[] blockPosSetSerialized = serializer.writeValueAsBytes(blockPosSet);
        final SmallBlockPosSetAABB blockPosSetDeserialized = serializer.readValue(blockPosSetSerialized, SmallBlockPosSetAABB.class);

        // Verify both sets are equal
        assertEquals(blockPosSet, blockPosSetDeserialized);
    }
}
