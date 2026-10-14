package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveServicePlanNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveServicePlanNoticeStore()
        val item = newAdaptiveServicePlanNotice("code-389", "label-747")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
