package com.acme.transport.emissions.emissionfactor.factormilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedEmissionFactorDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedEmissionFactorDraft("code-616", "label-668")
        assertEquals(CappedEmissionFactorDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedEmissionFactorDraft("code-616", "label-668")
        try {
            item.moveTo(CappedEmissionFactorDraftStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedEmissionFactorDraftStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedEmissionFactorDraft("code-616", "label-668")
        item.status = CappedEmissionFactorDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedEmissionFactorDraftStatus.REQUESTED) }
    }
}
