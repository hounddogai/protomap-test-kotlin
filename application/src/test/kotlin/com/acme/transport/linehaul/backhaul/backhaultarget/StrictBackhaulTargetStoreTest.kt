package com.acme.transport.linehaul.backhaul.backhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictBackhaulTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictBackhaulTargetStore()
        val item = newStrictBackhaulTarget("code-462", "title-647")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
