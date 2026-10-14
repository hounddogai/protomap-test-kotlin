package com.acme.transport.customs.hold.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newHoldAgreement("code-219", "label-640")
        assertEquals(HoldAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newHoldAgreement("code-219", "label-640")
        try {
            item.moveTo(HoldAgreementStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldAgreementStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newHoldAgreement("code-219", "label-640")
        item.status = HoldAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(HoldAgreementStatus.IDLE) }
    }
}
