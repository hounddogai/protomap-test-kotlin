package com.acme.transport.network.hub.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalHubPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalHubPolicyStore()
        val item = newGlobalHubPolicy("code-743", "title-94")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
