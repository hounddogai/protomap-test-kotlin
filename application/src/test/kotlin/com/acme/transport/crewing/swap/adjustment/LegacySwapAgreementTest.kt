package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacySwapAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacySwapAgreement("code-829", "label-214")
        assertEquals(LegacySwapAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLegacySwapAgreement("code-829", "label-214")
        try {
            item.moveTo(LegacySwapAgreementStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacySwapAgreementStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacySwapAgreement("code-829", "label-214")
        item.status = LegacySwapAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(LegacySwapAgreementStatus.IDLE) }
    }
}
