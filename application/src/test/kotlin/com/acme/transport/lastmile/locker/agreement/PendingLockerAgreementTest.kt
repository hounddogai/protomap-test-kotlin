package com.acme.transport.lastmile.locker.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingLockerAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newPendingLockerAgreement("code-531", "title-290")
        assertEquals(PendingLockerAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPendingLockerAgreement("code-531", "title-290")
        try {
            item.moveTo(PendingLockerAgreementStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingLockerAgreementStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingLockerAgreement("code-531", "title-290")
        item.status = PendingLockerAgreementStatus.FAILED
        assertFails("move") { item.moveTo(PendingLockerAgreementStatus.NEW) }
    }
}
