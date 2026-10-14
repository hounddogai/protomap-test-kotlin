package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingSwapLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingSwapLedgerEngine(newRegistry())
        val item = newRollingSwapLedger("code-938", "label-468")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
