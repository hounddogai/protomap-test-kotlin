package com.acme.transport.safety.nearmiss.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissProfileStore()
        val item = newNearMissProfile("code-972", "name-348")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
