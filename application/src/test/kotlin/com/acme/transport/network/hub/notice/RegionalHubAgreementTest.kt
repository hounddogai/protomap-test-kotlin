package com.acme.transport.network.hub.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalHubAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalHubAgreement("code-141", "name-246")
        assertEquals(RegionalHubAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalHubAgreement("code-141", "name-246")
        try {
            item.moveTo(RegionalHubAgreementStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalHubAgreementStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalHubAgreement("code-141", "name-246")
        item.status = RegionalHubAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalHubAgreementStatus.REQUESTED) }
    }
}
