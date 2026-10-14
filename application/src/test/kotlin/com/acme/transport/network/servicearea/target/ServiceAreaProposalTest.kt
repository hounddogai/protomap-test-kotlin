package com.acme.transport.network.servicearea.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaProposalTest {
    @Test
    fun `starts in new`() {
        val item = newServiceAreaProposal("code-227", "label-333")
        assertEquals(ServiceAreaProposalStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newServiceAreaProposal("code-227", "label-333")
        try {
            item.moveTo(ServiceAreaProposalStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaProposalStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newServiceAreaProposal("code-227", "label-333")
        item.status = ServiceAreaProposalStatus.FAILED
        assertFails("move") { item.moveTo(ServiceAreaProposalStatus.NEW) }
    }
}
