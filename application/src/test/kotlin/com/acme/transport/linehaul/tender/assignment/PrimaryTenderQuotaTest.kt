package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTenderQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryTenderQuota("code-674", "title-807")
        assertEquals(PrimaryTenderQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPrimaryTenderQuota("code-674", "title-807")
        try {
            item.moveTo(PrimaryTenderQuotaStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTenderQuotaStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryTenderQuota("code-674", "title-807")
        item.status = PrimaryTenderQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryTenderQuotaStatus.PROPOSED) }
    }
}
