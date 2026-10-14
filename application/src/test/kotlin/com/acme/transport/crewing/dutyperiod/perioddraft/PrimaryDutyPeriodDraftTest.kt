package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDutyPeriodDraftTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryDutyPeriodDraft("code-684", "label-379")
        assertEquals(PrimaryDutyPeriodDraftStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPrimaryDutyPeriodDraft("code-684", "label-379")
        try {
            item.moveTo(PrimaryDutyPeriodDraftStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDutyPeriodDraftStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryDutyPeriodDraft("code-684", "label-379")
        item.status = PrimaryDutyPeriodDraftStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryDutyPeriodDraftStatus.PROPOSED) }
    }
}
