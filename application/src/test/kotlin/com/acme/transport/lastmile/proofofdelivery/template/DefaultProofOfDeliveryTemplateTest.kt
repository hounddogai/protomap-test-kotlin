package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultProofOfDeliveryTemplateTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultProofOfDeliveryTemplate("code-266", "name-822")
        assertEquals(DefaultProofOfDeliveryTemplateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDefaultProofOfDeliveryTemplate("code-266", "name-822")
        try {
            item.moveTo(DefaultProofOfDeliveryTemplateStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultProofOfDeliveryTemplateStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultProofOfDeliveryTemplate("code-266", "name-822")
        item.status = DefaultProofOfDeliveryTemplateStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultProofOfDeliveryTemplateStatus.OPEN) }
    }
}
