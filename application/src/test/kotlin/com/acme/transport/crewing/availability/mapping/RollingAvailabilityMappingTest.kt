package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingAvailabilityMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingAvailabilityMapping("code-472", "name-182")
        assertEquals(RollingAvailabilityMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingAvailabilityMapping("code-472", "name-182")
        try {
            item.moveTo(RollingAvailabilityMappingStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingAvailabilityMappingStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingAvailabilityMapping("code-472", "name-182")
        item.status = RollingAvailabilityMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingAvailabilityMappingStatus.PROPOSED) }
    }
}
