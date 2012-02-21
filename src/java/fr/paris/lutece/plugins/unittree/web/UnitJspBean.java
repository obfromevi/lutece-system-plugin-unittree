/*
 * Copyright (c) 2002-2011, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.unittree.web;

import fr.paris.lutece.plugins.unittree.business.unit.Unit;
import fr.paris.lutece.plugins.unittree.service.action.IActionService;
import fr.paris.lutece.plugins.unittree.service.unit.IUnitService;
import fr.paris.lutece.plugins.unittree.web.action.IUnitAction;
import fr.paris.lutece.plugins.unittree.web.action.IUnitSearchFields;
import fr.paris.lutece.plugins.unittree.web.action.UnitSearchFields;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.html.XmlTransformerService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.web.admin.PluginAdminPageJspBean;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.portal.web.pluginaction.DefaultPluginActionResult;
import fr.paris.lutece.portal.web.pluginaction.IPluginActionResult;
import fr.paris.lutece.portal.web.pluginaction.PluginActionManager;
import fr.paris.lutece.util.UniqueIDGenerator;
import fr.paris.lutece.util.beanvalidation.BeanValidationUtil;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.validation.ConstraintViolation;

import javax.xml.transform.Source;


/**
 *
 * UnitJspBean
 *
 */
public class UnitJspBean extends PluginAdminPageJspBean
{
    public static final String RIGHT_MANAGE_UNITS = "UNITS_MANAGEMENT";

    // BEAN
    private static final String BEAN_UNIT_SERVICE = "unittree.unitService";
    private static final String BEAN_ACTION_SERVICE = "unittree.actionService";

    // PROPERTIES
    private static final String PROPERTY_MANAGE_UNITS_PAGE_TITLE = "unittree.manageUnits.pageTitle";
    private static final String PROPERTY_ERRROR_PAGE_TITLE = "unittree.error.pageTitle";
    private static final String PROPERTY_CREATE_UNIT_PAGE_TITLE = "unittree.createUnit.pageTitle";
    private static final String PROPERTY_MODIFY_UNIT_PAGE_TITLE = "unittree.modifyUnit.pageTitle";
    private static final String PROPERTY_ADD_USER_PAGE_TITLE = "unittree.addUser.pageTitle";

    // MESSAGES
    private static final String MESSAGE_ERROR_GENERIC_MESSAGE = "unittree.message.error.genericMessage";
    private static final String MESSAGE_ERROR_UNIT_NOT_FOUND = "unittree.message.error.unitNotFound";
    private static final String MESSAGE_ERROR_UNIT_HAS_SUB_UNITS = "unittree.message.error.unitHasSubUnits";
    private static final String MESSAGE_ERROR_USER_ALREADY_IN_AN_UNIT = "unittree.message.error.userAlreadyInAnUnit";
    private static final String MESSAGE_CONFIRM_REMOVE_UNIT = "unittree.message.removeUnit";

    // MARKS
    private static final String MARK_ERROR_MESSAGE = "errorMessage";
    private static final String MARK_ID_UNIT = "idUnit";
    private static final String MARK_UNIT_TREE = "unitTree";
    private static final String MARK_ID_PARENT = "idParent";
    private static final String MARK_LIST_SUB_UNITS = "listSubUnits";
    private static final String MARK_LIST_UNIT_ACTIONS = "listUnitActions";
    private static final String MARK_LIST_USERS = "listUsers";
    private static final String MARK_LIST_UNIT_USERS_ACTIONS = "listUnitUserActions";
    private static final String MARK_UNIT = "unit";

    // PARAMETERS
    private static final String PARAMETER_ERROR_MESSAGE = "errorMessage";
    private static final String PARAMETER_CANCEL = "cancel";
    private static final String PARAMETER_ID_UNIT = "idUnit";
    private static final String PARAMETER_ID_PARENT = "idParent";
    private static final String PARAMETER_ID_USERS = "idUsers";

    // TEMPLATES
    private static final String TEMPLATE_MANAGE_UNITS = "/admin/plugins/unittree/manage_units.html";
    private static final String TEMPLATE_ERROR = "/admin/plugins/unittree/error.html";
    private static final String TEMPLATE_CREATE_UNIT = "/admin/plugins/unittree/create_unit.html";
    private static final String TEMPLATE_MODIFY_UNIT = "/admin/plugins/unittree/modify_unit.html";
    private static final String TEMPLATE_ADD_USERS = "/admin/plugins/unittree/add_users.html";

    // JSP
    private static final String JSP_MANAGE_UNITS = "ManageUnits.jsp";
    private static final String JSP_ERROR = "Error.jsp";
    private static final String JSP_URL_DO_REMOVE_UNIT = "jsp/admin/plugins/unittree/DoRemoveUnit.jsp";
    private static final String UNIT_TREE_XSL_UNIQUE_PREFIX = UniqueIDGenerator.getNewId(  ) + "SpacesTree";
    private static final String XSL_PARAMETER_ID_CURRENT_UNIT = "id-current-unit";

    // SERVICES
    private IUnitService _unitService = (IUnitService) SpringContextService.getBean( BEAN_UNIT_SERVICE );
    private IActionService _actionService = (IActionService) SpringContextService.getBean( BEAN_ACTION_SERVICE );
    private IUnitSearchFields _searchFields = new UnitSearchFields(  );

    // GET

    /**
     * Get manage code mappings
     * @param request the HTTP request
     * @return the HTML code
     */
    public IPluginActionResult getManageUnits( HttpServletRequest request, HttpServletResponse response )
        throws AccessDeniedException
    {
        setPageTitleProperty( PROPERTY_MANAGE_UNITS_PAGE_TITLE );

        // first - see if there is an invoked action
        IUnitAction action = PluginActionManager.getPluginAction( request, IUnitAction.class );

        if ( action != null )
        {
            AppLogService.debug( "Processing unittree action " + action.getName(  ) );

            return action.process( request, response, getUser(  ), _searchFields );
        }

        // Get the selected unit
        String strIdUnit = request.getParameter( PARAMETER_ID_UNIT );
        Unit unit = null;

        if ( StringUtils.isNotBlank( strIdUnit ) && StringUtils.isNumeric( strIdUnit ) )
        {
            int nIdUnit = Integer.parseInt( strIdUnit );
            unit = _unitService.getUnit( nIdUnit );
        }

        if ( unit == null )
        {
            unit = _unitService.getRootUnit(  );
        }

        // Build the html for units tree
        String strXmlUnits = _unitService.getXMLUnits(  );
        Source sourceXsl = _unitService.getTreeXsl(  );
        Map<String, String> htXslParameters = new HashMap<String, String>(  );
        htXslParameters.put( XSL_PARAMETER_ID_CURRENT_UNIT, Integer.toString( unit.getIdUnit(  ) ) );

        XmlTransformerService xmlTransformerService = new XmlTransformerService(  );
        String strHtmlUnitsTree = xmlTransformerService.transformBySourceWithXslCache( strXmlUnits, sourceXsl,
                UNIT_TREE_XSL_UNIQUE_PREFIX, htXslParameters, null );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_UNIT_TREE, strHtmlUnitsTree );
        model.put( MARK_ID_UNIT, unit.getIdUnit(  ) );
        model.put( MARK_LIST_SUB_UNITS, _unitService.getSubUnits( unit.getIdUnit(  ) ) );
        model.put( MARK_LIST_UNIT_ACTIONS, _actionService.getListActions( Unit.RESOURCE_TYPE, getLocale(  ) ) );
        model.put( MARK_LIST_USERS, _unitService.getUsers( unit.getIdUnit(  ) ) );
        PluginActionManager.fillModel( request, getUser(  ), model, IUnitAction.class, MARK_LIST_UNIT_USERS_ACTIONS );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_UNITS, getLocale(  ), model );

        IPluginActionResult result = new DefaultPluginActionResult(  );
        result.setHtmlContent( getAdminPage( template.getHtml(  ) ) );

        return result;
    }

    public String getCreateUnit( HttpServletRequest request )
    {
        // TODO : Check permission
        setPageTitleProperty( PROPERTY_CREATE_UNIT_PAGE_TITLE );

        Map<String, Object> model = new HashMap<String, Object>(  );

        boolean bHasParent = false;
        String strIdParent = request.getParameter( PARAMETER_ID_UNIT );

        if ( StringUtils.isNotBlank( strIdParent ) && StringUtils.isNumeric( strIdParent ) )
        {
            int nIdParent = Integer.parseInt( strIdParent );
            Unit unitParent = _unitService.getUnit( nIdParent );

            if ( unitParent != null )
            {
                model.put( MARK_ID_PARENT, strIdParent );
                bHasParent = true;
            }
        }

        if ( !bHasParent )
        {
            model.put( MARK_ID_PARENT, Unit.ID_ROOT );
        }

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_UNIT, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    public String getModifyUnit( HttpServletRequest request )
    {
        // TODO : Check permission
        setPageTitleProperty( PROPERTY_MODIFY_UNIT_PAGE_TITLE );

        Unit unit = null;
        String strIdUnit = request.getParameter( PARAMETER_ID_UNIT );

        if ( StringUtils.isNotBlank( strIdUnit ) && StringUtils.isNumeric( strIdUnit ) )
        {
            int nIdParent = Integer.parseInt( strIdUnit );
            unit = _unitService.getUnit( nIdParent );
        }

        if ( unit == null )
        {
            return getError( request );
        }

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_UNIT, unit );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_UNIT, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    public String getAddUser( HttpServletRequest request )
    {
        // TODO : Check permission
        setPageTitleProperty( PROPERTY_ADD_USER_PAGE_TITLE );

        // Get the selected unit
        String strIdUnit = request.getParameter( PARAMETER_ID_UNIT );
        Unit unit = null;

        if ( StringUtils.isNotBlank( strIdUnit ) && StringUtils.isNumeric( strIdUnit ) )
        {
            int nIdUnit = Integer.parseInt( strIdUnit );
            unit = _unitService.getUnit( nIdUnit );
        }

        if ( unit == null )
        {
            unit = _unitService.getRootUnit(  );
        }

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_UNIT, unit );
        model.put( MARK_LIST_USERS, _unitService.getUsers( getUser(  ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_ADD_USERS, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Get confirm remove code mapping
     * @param request the HTTP request
     * @return the HTML code
     */
    public String getConfirmRemoveUnit( HttpServletRequest request )
    {
        String strIdUnit = request.getParameter( PARAMETER_ID_UNIT );

        if ( StringUtils.isBlank( strIdUnit ) || !StringUtils.isNumeric( strIdUnit ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        UrlItem url = new UrlItem( JSP_URL_DO_REMOVE_UNIT );
        url.addParameter( PARAMETER_ID_UNIT, strIdUnit );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_UNIT, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Get error
     * @param request the HTTP request
     * @return the HTML code
     */
    public String getError( HttpServletRequest request )
    {
        String strErrorMessage = request.getParameter( PARAMETER_ERROR_MESSAGE );

        if ( StringUtils.isBlank( strErrorMessage ) )
        {
            strErrorMessage = I18nService.getLocalizedString( MESSAGE_ERROR_GENERIC_MESSAGE, getLocale(  ) );
        }

        return getError( request, strErrorMessage );
    }

    /**
     * Get error
     * @param request the HTTP request
     * @param strErrorMessage the error message
     * @return the HTML code
     */
    public String getError( HttpServletRequest request, String strErrorMessage )
    {
        setPageTitleProperty( PROPERTY_ERRROR_PAGE_TITLE );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_ERROR_MESSAGE, strErrorMessage );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_ERROR, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    // DO
    public String doCreateUnit( HttpServletRequest request )
    {
        String strCancel = request.getParameter( PARAMETER_CANCEL );
        String strIdParent = request.getParameter( PARAMETER_ID_PARENT );

        if ( StringUtils.isNotBlank( strCancel ) )
        {
            UrlItem url = new UrlItem( JSP_MANAGE_UNITS );
            url.addParameter( PARAMETER_ID_UNIT, strIdParent );

            return url.getUrl(  );
        }

        Unit unit = new Unit(  );
        // Populate the bean
        populate( unit, request );

        // Check mandatory fields
        Set<ConstraintViolation<Unit>> constraintViolations = BeanValidationUtil.validate( unit );

        if ( constraintViolations.size(  ) > 0 )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        try
        {
            _unitService.createUnit( unit );
        }
        catch ( Exception ex )
        {
            // Something wrong happened... a database check might be needed
            AppLogService.error( ex.getMessage(  ) + " when creating an unit ", ex );
            // Revert
            _unitService.removeUnit( unit.getIdUnit(  ) );

            return JSP_ERROR;
        }

        UrlItem url = new UrlItem( JSP_MANAGE_UNITS );
        url.addParameter( PARAMETER_ID_UNIT, unit.getIdUnit(  ) );

        return url.getUrl(  );
    }

    public String doModifyUnit( HttpServletRequest request )
    {
        String strCancel = request.getParameter( PARAMETER_CANCEL );
        String strIdUnit = request.getParameter( PARAMETER_ID_UNIT );

        if ( StringUtils.isNotBlank( strCancel ) )
        {
            UrlItem url = new UrlItem( JSP_MANAGE_UNITS );
            url.addParameter( PARAMETER_ID_UNIT, strIdUnit );

            return url.getUrl(  );
        }

        if ( StringUtils.isBlank( strIdUnit ) || !StringUtils.isNumeric( strIdUnit ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        int nIdUnit = Integer.parseInt( strIdUnit );
        Unit unit = _unitService.getUnit( nIdUnit );

        if ( unit == null )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_UNIT_NOT_FOUND, AdminMessage.TYPE_STOP );
        }

        // Populate the bean
        populate( unit, request );

        // Check mandatory fields
        Set<ConstraintViolation<Unit>> constraintViolations = BeanValidationUtil.validate( unit );

        if ( constraintViolations.size(  ) > 0 )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        try
        {
            _unitService.updateUnit( unit );
        }
        catch ( Exception ex )
        {
            // Something wrong happened... a database check might be needed
            AppLogService.error( ex.getMessage(  ) + " when modifying an unit ", ex );

            return JSP_ERROR;
        }

        UrlItem url = new UrlItem( JSP_MANAGE_UNITS );
        url.addParameter( PARAMETER_ID_UNIT, unit.getIdUnit(  ) );

        return url.getUrl(  );
    }

    public String doRemoveUnit( HttpServletRequest request )
    {
        String strIdUnit = request.getParameter( PARAMETER_ID_UNIT );

        if ( StringUtils.isBlank( strIdUnit ) || !StringUtils.isNumeric( strIdUnit ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        int nIdUnit = Integer.parseInt( strIdUnit );
        int nIdParent = Unit.ID_ROOT;
        Unit unit = _unitService.getUnit( nIdUnit );

        if ( unit != null )
        {
            if ( _unitService.hasSubUnits( nIdUnit ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_UNIT_HAS_SUB_UNITS,
                    AdminMessage.TYPE_STOP );
            }

            nIdParent = unit.getIdParent(  );

            try
            {
                _unitService.removeUnit( nIdUnit );
            }
            catch ( Exception ex )
            {
                // Something wrong happened... a database check might be needed
                AppLogService.error( ex.getMessage(  ) + " when deleting an unit ", ex );

                return JSP_ERROR;
            }
        }

        UrlItem url = new UrlItem( JSP_MANAGE_UNITS );
        url.addParameter( PARAMETER_ID_UNIT, nIdParent );

        return url.getUrl(  );
    }

    public String doAddUsers( HttpServletRequest request )
    {
        String strCancel = request.getParameter( PARAMETER_CANCEL );
        String strIdUnit = request.getParameter( PARAMETER_ID_UNIT );

        if ( StringUtils.isNotBlank( strCancel ) )
        {
            UrlItem url = new UrlItem( JSP_MANAGE_UNITS );
            url.addParameter( PARAMETER_ID_UNIT, strIdUnit );

            return url.getUrl(  );
        }

        String[] listIdUsers = request.getParameterValues( PARAMETER_ID_USERS );

        if ( ( listIdUsers == null ) || ( listIdUsers.length == 0 ) || StringUtils.isBlank( strIdUnit ) ||
                !StringUtils.isNumeric( strIdUnit ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        int nIdUnit = Integer.parseInt( strIdUnit );

        for ( String strIdUser : listIdUsers )
        {
            if ( StringUtils.isNotBlank( strIdUser ) && StringUtils.isNumeric( strIdUser ) )
            {
                int nIdUser = Integer.parseInt( strIdUser );

                // TODO : Check existence before adding, otherwise some data will be stored even when there are errors 
                if ( !_unitService.addUserToUnit( nIdUnit, nIdUser ) )
                {
                    return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_USER_ALREADY_IN_AN_UNIT,
                        AdminMessage.TYPE_STOP );
                }
            }
        }

        UrlItem url = new UrlItem( JSP_MANAGE_UNITS );
        url.addParameter( PARAMETER_ID_UNIT, nIdUnit );

        return url.getUrl(  );
    }
}
