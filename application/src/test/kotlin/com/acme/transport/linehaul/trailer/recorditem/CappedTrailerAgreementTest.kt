package com.acme.transport.linehaul.trailer.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTrailerAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedTrailerAgreement("code-64", "title-164")
        assertEquals(CappedTrailerAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newCappedTrailerAgreement("code-64", "title-164")
        try {
            item.moveTo(CappedTrailerAgreementStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTrailerAgreementStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedTrailerAgreement("code-64", "title-164")
        item.status = CappedTrailerAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(CappedTrailerAgreementStatus.IDLE) }
    }
}
