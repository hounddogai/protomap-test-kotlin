package com.acme.transport.network.servicearea.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newServiceAreaTemplate("code-153", "title-659")
        assertEquals(ServiceAreaTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newServiceAreaTemplate("code-153", "title-659")
        try {
            item.moveTo(ServiceAreaTemplateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaTemplateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newServiceAreaTemplate("code-153", "title-659")
        item.status = ServiceAreaTemplateStatus.FAILED
        assertFails("move") { item.moveTo(ServiceAreaTemplateStatus.NEW) }
    }
}
