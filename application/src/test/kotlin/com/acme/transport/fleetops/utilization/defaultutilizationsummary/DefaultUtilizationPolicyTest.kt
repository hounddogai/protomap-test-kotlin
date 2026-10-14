package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultUtilizationPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultUtilizationPolicy("code-274", "title-608")
        assertEquals(DefaultUtilizationPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDefaultUtilizationPolicy("code-274", "title-608")
        try {
            item.moveTo(DefaultUtilizationPolicyStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultUtilizationPolicyStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultUtilizationPolicy("code-274", "title-608")
        item.status = DefaultUtilizationPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultUtilizationPolicyStatus.OPEN) }
    }
}
