package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial templates by code. */
interface AccessorialTemplateStore {
    fun find(code: String): AccessorialTemplate?
    fun save(item: AccessorialTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialTemplateStore(
    var items: MutableMap<String, AccessorialTemplate>,
    var order: MutableList<String>,
) : AccessorialTemplateStore {
    override fun find(code: String): AccessorialTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialTemplate) {
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

    override fun list(): MutableList<AccessorialTemplate> {
        val out = mutableListOf<AccessorialTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialTemplateStore(): AccessorialTemplateStore {
    return MemoryAccessorialTemplateStore(mutableMapOf<String, AccessorialTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialTemplateStore(
    var inner: AccessorialTemplateStore,
    var cache: TtlCache<AccessorialTemplate>,
    var hits: Int,
    var misses: Int,
) : AccessorialTemplateStore {
    override fun find(code: String): AccessorialTemplate? {
        val hit = (cache.get(code) as AccessorialTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialTemplate> = inner.list()
}

fun newCachedAccessorialTemplateStore(inner: AccessorialTemplateStore, ttl: Int, clock: Clock): AccessorialTemplateStore {
    return CachedAccessorialTemplateStore(
        inner = inner,
        cache = newTtlCache<AccessorialTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial templates. Empty fields match all. */
data class AccessorialTemplateQuery(
    var channel: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialTemplate): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialTemplate>): MutableList<AccessorialTemplate> {
        val matched = filter(items) { candidate: AccessorialTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialTemplates(store: AccessorialTemplateStore, query: AccessorialTemplateQuery): MutableList<AccessorialTemplate> {
    return query.apply(store.list())
}
