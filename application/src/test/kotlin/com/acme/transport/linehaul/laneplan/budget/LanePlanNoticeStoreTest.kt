package com.acme.transport.linehaul.laneplan.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanNoticeStore()
        val item = newLanePlanNotice("code-505", "name-556")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
