package com.acme.transport.crewing.dutyperiod.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodBundleTest {
    @Test
    fun `starts in new`() {
        val item = newDutyPeriodBundle("code-777", "name-634")
        assertEquals(DutyPeriodBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newDutyPeriodBundle("code-777", "name-634")
        try {
            item.moveTo(DutyPeriodBundleStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodBundleStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDutyPeriodBundle("code-777", "name-634")
        item.status = DutyPeriodBundleStatus.FAILED
        assertFails("move") { item.moveTo(DutyPeriodBundleStatus.NEW) }
    }
}
