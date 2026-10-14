package com.acme.transport.emissions.idling.idlingquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualIdlingTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualIdlingTargetStore()
        val item = newManualIdlingTarget("code-695", "title-529")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
