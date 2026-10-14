package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay profiles by code. */
interface RelayProfileStore {
    fun find(code: String): RelayProfile?
    fun save(item: RelayProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayProfileStore(
    var items: MutableMap<String, RelayProfile>,
    var order: MutableList<String>,
) : RelayProfileStore {
    override fun find(code: String): RelayProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayProfile) {
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

    override fun list(): MutableList<RelayProfile> {
        val out = mutableListOf<RelayProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayProfileStore(): RelayProfileStore {
    return MemoryRelayProfileStore(mutableMapOf<String, RelayProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayProfileStore(
    var inner: RelayProfileStore,
    var cache: TtlCache<RelayProfile>,
    var hits: Int,
    var misses: Int,
) : RelayProfileStore {
    override fun find(code: String): RelayProfile? {
        val hit = (cache.get(code) as RelayProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayProfile> = inner.list()
}

fun newCachedRelayProfileStore(inner: RelayProfileStore, ttl: Int, clock: Clock): RelayProfileStore {
    return CachedRelayProfileStore(
        inner = inner,
        cache = newTtlCache<RelayProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay profiles. Empty fields match all. */
data class RelayProfileQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayProfile>): MutableList<RelayProfile> {
        val matched = filter(items) { candidate: RelayProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayProfiles(store: RelayProfileStore, query: RelayProfileQuery): MutableList<RelayProfile> {
    return query.apply(store.list())
}
