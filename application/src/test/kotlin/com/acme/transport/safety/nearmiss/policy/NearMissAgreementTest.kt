package com.acme.transport.safety.nearmiss.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newNearMissAgreement("code-34", "name-30")
        assertEquals(NearMissAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newNearMissAgreement("code-34", "name-30")
        try {
            item.moveTo(NearMissAgreementStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissAgreementStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newNearMissAgreement("code-34", "name-30")
        item.status = NearMissAgreementStatus.FAILED
        assertFails("move") { item.moveTo(NearMissAgreementStatus.NEW) }
    }
}
