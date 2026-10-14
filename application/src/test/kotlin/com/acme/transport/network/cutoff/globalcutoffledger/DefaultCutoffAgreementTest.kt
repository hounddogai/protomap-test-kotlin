package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCutoffAgreementTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultCutoffAgreement("code-555", "title-911")
        assertEquals(DefaultCutoffAgreementStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDefaultCutoffAgreement("code-555", "title-911")
        try {
            item.moveTo(DefaultCutoffAgreementStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCutoffAgreementStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultCutoffAgreement("code-555", "title-911")
        item.status = DefaultCutoffAgreementStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultCutoffAgreementStatus.PLANNED) }
    }
}
