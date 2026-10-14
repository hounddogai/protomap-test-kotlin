package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLockerAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledLockerAgreement("code-500", "title-694")
        assertEquals(ScheduledLockerAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newScheduledLockerAgreement("code-500", "title-694")
        try {
            item.moveTo(ScheduledLockerAgreementStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLockerAgreementStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledLockerAgreement("code-500", "title-694")
        item.status = ScheduledLockerAgreementStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledLockerAgreementStatus.NEW) }
    }
}
