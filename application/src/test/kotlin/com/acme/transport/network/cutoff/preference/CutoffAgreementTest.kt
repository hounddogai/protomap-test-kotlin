package com.acme.transport.network.cutoff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newCutoffAgreement("code-854", "title-697")
        assertEquals(CutoffAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCutoffAgreement("code-854", "title-697")
        try {
            item.moveTo(CutoffAgreementStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffAgreementStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCutoffAgreement("code-854", "title-697")
        item.status = CutoffAgreementStatus.FAILED
        assertFails("move") { item.moveTo(CutoffAgreementStatus.NEW) }
    }
}
