package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingAccessorialPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingAccessorialPolicyEngine(newRegistry())
        val item = newRollingAccessorialPolicy("code-943", "label-235")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
