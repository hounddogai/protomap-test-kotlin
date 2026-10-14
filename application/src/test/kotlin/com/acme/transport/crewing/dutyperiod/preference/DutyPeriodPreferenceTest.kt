package com.acme.transport.crewing.dutyperiod.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newDutyPeriodPreference("code-132", "title-328")
        assertEquals(DutyPeriodPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDutyPeriodPreference("code-132", "title-328")
        try {
            item.moveTo(DutyPeriodPreferenceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodPreferenceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDutyPeriodPreference("code-132", "title-328")
        item.status = DutyPeriodPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(DutyPeriodPreferenceStatus.NEW) }
    }
}
