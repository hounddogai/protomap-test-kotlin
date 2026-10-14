package com.acme.transport.linehaul.relay.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay digests by code. */
interface RelayDigestStore {
    fun find(code: String): RelayDigest?
    fun save(item: RelayDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayDigestStore(
    var items: MutableMap<String, RelayDigest>,
    var order: MutableList<String>,
) : RelayDigestStore {
    override fun find(code: String): RelayDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayDigest) {
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

    override fun list(): MutableList<RelayDigest> {
        val out = mutableListOf<RelayDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayDigestStore(): RelayDigestStore {
    return MemoryRelayDigestStore(mutableMapOf<String, RelayDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayDigestStore(
    var inner: RelayDigestStore,
    var cache: TtlCache<RelayDigest>,
    var hits: Int,
    var misses: Int,
) : RelayDigestStore {
    override fun find(code: String): RelayDigest? {
        val hit = (cache.get(code) as RelayDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayDigest> = inner.list()
}

fun newCachedRelayDigestStore(inner: RelayDigestStore, ttl: Int, clock: Clock): RelayDigestStore {
    return CachedRelayDigestStore(
        inner = inner,
        cache = newTtlCache<RelayDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay digests. Empty fields match all. */
data class RelayDigestQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayDigest>): MutableList<RelayDigest> {
        val matched = filter(items) { candidate: RelayDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayDigests(store: RelayDigestStore, query: RelayDigestQuery): MutableList<RelayDigest> {
    return query.apply(store.list())
}
