package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newTenderAgreement("code-456", "title-2")
        assertEquals(TenderAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTenderAgreement("code-456", "title-2")
        try {
            item.moveTo(TenderAgreementStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderAgreementStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTenderAgreement("code-456", "title-2")
        item.status = TenderAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(TenderAgreementStatus.CREATED) }
    }
}
