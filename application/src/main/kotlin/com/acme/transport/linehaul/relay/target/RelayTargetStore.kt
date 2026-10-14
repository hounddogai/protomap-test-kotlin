package com.acme.transport.linehaul.relay.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay targets by code. */
interface RelayTargetStore {
    fun find(code: String): RelayTarget?
    fun save(item: RelayTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayTargetStore(
    var items: MutableMap<String, RelayTarget>,
    var order: MutableList<String>,
) : RelayTargetStore {
    override fun find(code: String): RelayTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayTarget) {
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

    override fun list(): MutableList<RelayTarget> {
        val out = mutableListOf<RelayTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayTargetStore(): RelayTargetStore {
    return MemoryRelayTargetStore(mutableMapOf<String, RelayTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayTargetStore(
    var inner: RelayTargetStore,
    var cache: TtlCache<RelayTarget>,
    var hits: Int,
    var misses: Int,
) : RelayTargetStore {
    override fun find(code: String): RelayTarget? {
        val hit = (cache.get(code) as RelayTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayTarget> = inner.list()
}

fun newCachedRelayTargetStore(inner: RelayTargetStore, ttl: Int, clock: Clock): RelayTargetStore {
    return CachedRelayTargetStore(
        inner = inner,
        cache = newTtlCache<RelayTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay targets. Empty fields match all. */
data class RelayTargetQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayTarget>): MutableList<RelayTarget> {
        val matched = filter(items) { candidate: RelayTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayTargets(store: RelayTargetStore, query: RelayTargetQuery): MutableList<RelayTarget> {
    return query.apply(store.list())
}
