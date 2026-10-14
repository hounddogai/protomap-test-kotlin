package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay cycles by code. */
interface RelayCycleStore {
    fun find(code: String): RelayCycle?
    fun save(item: RelayCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayCycleStore(
    var items: MutableMap<String, RelayCycle>,
    var order: MutableList<String>,
) : RelayCycleStore {
    override fun find(code: String): RelayCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayCycle) {
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

    override fun list(): MutableList<RelayCycle> {
        val out = mutableListOf<RelayCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayCycleStore(): RelayCycleStore {
    return MemoryRelayCycleStore(mutableMapOf<String, RelayCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayCycleStore(
    var inner: RelayCycleStore,
    var cache: TtlCache<RelayCycle>,
    var hits: Int,
    var misses: Int,
) : RelayCycleStore {
    override fun find(code: String): RelayCycle? {
        val hit = (cache.get(code) as RelayCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayCycle> = inner.list()
}

fun newCachedRelayCycleStore(inner: RelayCycleStore, ttl: Int, clock: Clock): RelayCycleStore {
    return CachedRelayCycleStore(
        inner = inner,
        cache = newTtlCache<RelayCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay cycles. Empty fields match all. */
data class RelayCycleQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayCycle>): MutableList<RelayCycle> {
        val matched = filter(items) { candidate: RelayCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayCycles(store: RelayCycleStore, query: RelayCycleQuery): MutableList<RelayCycle> {
    return query.apply(store.list())
}
