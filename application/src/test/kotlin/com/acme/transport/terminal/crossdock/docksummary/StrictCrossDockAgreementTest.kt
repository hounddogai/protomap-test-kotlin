package com.acme.transport.terminal.crossdock.docksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCrossDockAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictCrossDockAgreement("code-767", "title-915")
        assertEquals(StrictCrossDockAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newStrictCrossDockAgreement("code-767", "title-915")
        try {
            item.moveTo(StrictCrossDockAgreementStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCrossDockAgreementStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictCrossDockAgreement("code-767", "title-915")
        item.status = StrictCrossDockAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(StrictCrossDockAgreementStatus.DRAFT) }
    }
}
