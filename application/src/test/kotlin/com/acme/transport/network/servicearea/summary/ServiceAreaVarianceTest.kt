package com.acme.transport.network.servicearea.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newServiceAreaVariance("code-98", "name-554")
        assertEquals(ServiceAreaVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newServiceAreaVariance("code-98", "name-554")
        try {
            item.moveTo(ServiceAreaVarianceStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaVarianceStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newServiceAreaVariance("code-98", "name-554")
        item.status = ServiceAreaVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(ServiceAreaVarianceStatus.OPEN) }
    }
}
