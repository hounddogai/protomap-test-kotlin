package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryServicePlanNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryServicePlanNoticeStore()
        val item = newPrimaryServicePlanNotice("code-72", "label-196")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
