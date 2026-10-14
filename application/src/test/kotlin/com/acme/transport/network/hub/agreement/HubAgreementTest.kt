package com.acme.transport.network.hub.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newHubAgreement("code-816", "label-266")
        assertEquals(HubAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newHubAgreement("code-816", "label-266")
        try {
            item.moveTo(HubAgreementStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubAgreementStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newHubAgreement("code-816", "label-266")
        item.status = HubAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(HubAgreementStatus.DRAFT) }
    }
}
