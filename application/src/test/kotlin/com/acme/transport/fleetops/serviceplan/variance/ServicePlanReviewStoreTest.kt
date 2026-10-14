package com.acme.transport.fleetops.serviceplan.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanReviewStore()
        val item = newServicePlanReview("code-469", "label-197")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
