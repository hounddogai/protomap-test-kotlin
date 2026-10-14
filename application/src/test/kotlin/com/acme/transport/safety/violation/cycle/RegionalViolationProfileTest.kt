package com.acme.transport.safety.violation.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalViolationProfileTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalViolationProfile("code-389", "name-67")
        assertEquals(RegionalViolationProfileStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRegionalViolationProfile("code-389", "name-67")
        try {
            item.moveTo(RegionalViolationProfileStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalViolationProfileStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalViolationProfile("code-389", "name-67")
        item.status = RegionalViolationProfileStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalViolationProfileStatus.IDLE) }
    }
}
