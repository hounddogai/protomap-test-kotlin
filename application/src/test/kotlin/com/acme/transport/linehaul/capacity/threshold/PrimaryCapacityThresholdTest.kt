package com.acme.transport.linehaul.capacity.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCapacityThresholdTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryCapacityThreshold("code-52", "name-160")
        assertEquals(PrimaryCapacityThresholdStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPrimaryCapacityThreshold("code-52", "name-160")
        try {
            item.moveTo(PrimaryCapacityThresholdStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCapacityThresholdStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryCapacityThreshold("code-52", "name-160")
        item.status = PrimaryCapacityThresholdStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryCapacityThresholdStatus.CREATED) }
    }
}
