package com.acme.transport.customs.declaration.review

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration templates by code. */
interface DeclarationTemplateStore {
    fun find(code: String): DeclarationTemplate?
    fun save(item: DeclarationTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationTemplateStore(
    var items: MutableMap<String, DeclarationTemplate>,
    var order: MutableList<String>,
) : DeclarationTemplateStore {
    override fun find(code: String): DeclarationTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationTemplate) {
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

    override fun list(): MutableList<DeclarationTemplate> {
        val out = mutableListOf<DeclarationTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationTemplateStore(): DeclarationTemplateStore {
    return MemoryDeclarationTemplateStore(mutableMapOf<String, DeclarationTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationTemplateStore(
    var inner: DeclarationTemplateStore,
    var cache: TtlCache<DeclarationTemplate>,
    var hits: Int,
    var misses: Int,
) : DeclarationTemplateStore {
    override fun find(code: String): DeclarationTemplate? {
        val hit = (cache.get(code) as DeclarationTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationTemplate> = inner.list()
}

fun newCachedDeclarationTemplateStore(inner: DeclarationTemplateStore, ttl: Int, clock: Clock): DeclarationTemplateStore {
    return CachedDeclarationTemplateStore(
        inner = inner,
        cache = newTtlCache<DeclarationTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration templates. Empty fields match all. */
data class DeclarationTemplateQuery(
    var source: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationTemplate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationTemplate>): MutableList<DeclarationTemplate> {
        val matched = filter(items) { candidate: DeclarationTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationTemplates(store: DeclarationTemplateStore, query: DeclarationTemplateQuery): MutableList<DeclarationTemplate> {
    return query.apply(store.list())
}
