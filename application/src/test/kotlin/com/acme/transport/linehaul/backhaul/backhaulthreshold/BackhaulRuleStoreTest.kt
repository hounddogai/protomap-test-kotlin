package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulRuleStore()
        val item = newBackhaulRule("code-313", "name-931")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
