package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryMappingTest {
    @Test
    fun `starts in created`() {
        val item = newProofOfDeliveryMapping("code-751", "name-893")
        assertEquals(ProofOfDeliveryMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newProofOfDeliveryMapping("code-751", "name-893")
        try {
            item.moveTo(ProofOfDeliveryMappingStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryMappingStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newProofOfDeliveryMapping("code-751", "name-893")
        item.status = ProofOfDeliveryMappingStatus.RETIRED
        assertFails("move") { item.moveTo(ProofOfDeliveryMappingStatus.CREATED) }
    }
}
