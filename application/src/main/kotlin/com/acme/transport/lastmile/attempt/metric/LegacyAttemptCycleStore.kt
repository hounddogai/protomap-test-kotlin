package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy attempt cycles by code. */
interface LegacyAttemptCycleStore {
    fun find(code: String): LegacyAttemptCycle?
    fun save(item: LegacyAttemptCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAttemptCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAttemptCycleStore(
    var items: MutableMap<String, LegacyAttemptCycle>,
    var order: MutableList<String>,
) : LegacyAttemptCycleStore {
    override fun find(code: String): LegacyAttemptCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAttemptCycle) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<LegacyAttemptCycle> {
        val out = mutableListOf<LegacyAttemptCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAttemptCycleStore(): LegacyAttemptCycleStore {
    return MemoryLegacyAttemptCycleStore(mutableMapOf<String, LegacyAttemptCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAttemptCycleStore(
    var inner: LegacyAttemptCycleStore,
    var cache: TtlCache<LegacyAttemptCycle>,
    var hits: Int,
    var misses: Int,
) : LegacyAttemptCycleStore {
    override fun find(code: String): LegacyAttemptCycle? {
        val hit = (cache.get(code) as LegacyAttemptCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAttemptCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAttemptCycle> = inner.list()
}

fun newCachedLegacyAttemptCycleStore(inner: LegacyAttemptCycleStore, ttl: Int, clock: Clock): LegacyAttemptCycleStore {
    return CachedLegacyAttemptCycleStore(
        inner = inner,
        cache = newTtlCache<LegacyAttemptCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy attempt cycles. Empty fields match all. */
data class LegacyAttemptCycleQuery(
    var channel: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAttemptCycle): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyAttemptCycle>): MutableList<LegacyAttemptCycle> {
        val matched = filter(items) { candidate: LegacyAttemptCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAttemptCycles(store: LegacyAttemptCycleStore, query: LegacyAttemptCycleQuery): MutableList<LegacyAttemptCycle> {
    return query.apply(store.list())
}
