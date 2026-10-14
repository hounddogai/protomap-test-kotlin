package com.acme.transport.network.hub.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalHubPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalHubPolicyEngine(newRegistry())
        val item = newGlobalHubPolicy("code-743", "title-94")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
