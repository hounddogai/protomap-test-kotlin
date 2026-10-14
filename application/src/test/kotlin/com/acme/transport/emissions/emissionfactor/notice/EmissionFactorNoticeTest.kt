package com.acme.transport.emissions.emissionfactor.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorNoticeTest {
    @Test
    fun `starts in open`() {
        val item = newEmissionFactorNotice("code-819", "label-472")
        assertEquals(EmissionFactorNoticeStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newEmissionFactorNotice("code-819", "label-472")
        try {
            item.moveTo(EmissionFactorNoticeStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorNoticeStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEmissionFactorNotice("code-819", "label-472")
        item.status = EmissionFactorNoticeStatus.ARCHIVED
        assertFails("move") { item.moveTo(EmissionFactorNoticeStatus.OPEN) }
    }
}
