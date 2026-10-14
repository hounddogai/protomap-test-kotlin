package com.acme.transport.crewing.swap.swapmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedSwapAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedSwapAgreement("code-443", "name-340")
        assertEquals(CappedSwapAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCappedSwapAgreement("code-443", "name-340")
        try {
            item.moveTo(CappedSwapAgreementStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedSwapAgreementStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedSwapAgreement("code-443", "name-340")
        item.status = CappedSwapAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(CappedSwapAgreementStatus.IDLE) }
    }
}
