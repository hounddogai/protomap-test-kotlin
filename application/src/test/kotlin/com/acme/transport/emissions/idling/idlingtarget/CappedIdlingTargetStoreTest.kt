package com.acme.transport.emissions.idling.idlingtarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedIdlingTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedIdlingTargetStore()
        val item = newCappedIdlingTarget("code-705", "label-605")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
