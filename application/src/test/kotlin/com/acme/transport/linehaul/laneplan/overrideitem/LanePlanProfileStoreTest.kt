package com.acme.transport.linehaul.laneplan.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanProfileStore()
        val item = newLanePlanProfile("code-296", "label-924")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
