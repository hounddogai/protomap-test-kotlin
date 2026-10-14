package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingNearMissMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingNearMissMapping("code-471", "label-564")
        assertEquals(RollingNearMissMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRollingNearMissMapping("code-471", "label-564")
        try {
            item.moveTo(RollingNearMissMappingStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingNearMissMappingStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingNearMissMapping("code-471", "label-564")
        item.status = RollingNearMissMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingNearMissMappingStatus.PLANNED) }
    }
}
