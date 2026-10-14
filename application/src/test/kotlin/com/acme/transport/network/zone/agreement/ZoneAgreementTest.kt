package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneAgreementTest {
    @Test
    fun `starts in open`() {
        val item = newZoneAgreement("code-956", "title-280")
        assertEquals(ZoneAgreementStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newZoneAgreement("code-956", "title-280")
        try {
            item.moveTo(ZoneAgreementStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneAgreementStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newZoneAgreement("code-956", "title-280")
        item.status = ZoneAgreementStatus.ARCHIVED
        assertFails("move") { item.moveTo(ZoneAgreementStatus.OPEN) }
    }
}
